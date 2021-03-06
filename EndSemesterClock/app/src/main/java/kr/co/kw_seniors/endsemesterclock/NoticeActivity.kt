package kr.co.kw_seniors.endsemesterclock

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kr.co.kw_seniors.endsemesterclock.databinding.ActivityNoticeBinding
import org.jsoup.Jsoup

class NoticeActivity : AppCompatActivity() {

    companion object{
        /* 일반: 0, 학사: 1, 학생: 2, 등록/장학: 4 */
        // 각 공지사항의 1페이지 주소 = PAGE1_FRONT_BASE_URL + 카테고리 넘버 + PAGE1_BACK_BASE_URL
        // 예: https://www.kw.ac.kr/ko/life/notice.jsp?srCategoryId=0&mode=list&searchKey=1&searchVal= - 일반 공지의 1페이지
        const val PAGE1_FRONT_BASE_URL = "https://www.kw.ac.kr/ko/life/notice.jsp?srCategoryId="
        const val PAGE1_BACK_BASE_URL = "&mode=list&searchKey=1&searchVal="
        // 2페이지 이후 주소 = AFTER_PAGE2_FRONT_BASE_URL + 페이지 넘버 + AFTER_PAGE2_BACK_BASE_URL + 카테고리 넘버
        // 예: https://www.kw.ac.kr/ko/life/notice.jsp?MaxRows=10&tpage=3&searchKey=1&searchVal=&srCategoryId=4 - 등록/장학 공지의 3페이지
        const val AFTER_PAGE2_FRONT_BASE_URL = "https://www.kw.ac.kr/ko/life/notice.jsp?MaxRows=10&tpage="
        const val AFTER_PAGE2_BACK_BASE_URL = "&searchKey=1&searchVal=&srCategoryId="
        // 가져올 페이지 수
        const val MAX_PAGE = 10
        // HTML 문서 내에서 공지사항 아이템 태그의 경로
        const val ITEM_ROUTE = "div.notice div.list-box div.board-list-box ul li div"
    }
    // 레이아웃 바인딩
    val binding by lazy{ActivityNoticeBinding.inflate(layoutInflater)}
    // 프래그먼트
    lateinit var commonNoticeFragment: CommonNoticeFragment
    lateinit var bachelorNoticeFragment: BachelorNoticeFragment
    lateinit var studentNoticeFragment: StudentNoticeFragment
    lateinit var enrollNoticeFragment: EnrollNoticeFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 탭 레이아웃
        commonNoticeFragment = CommonNoticeFragment()
        bachelorNoticeFragment = BachelorNoticeFragment()
        studentNoticeFragment = StudentNoticeFragment()
        enrollNoticeFragment = EnrollNoticeFragment()
        // 처음 시작할 때 보이는 프래그먼트
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, commonNoticeFragment).commit()
        // 탭 리스너
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> {
                        replaceView(commonNoticeFragment)
                    }
                    1 -> {
                        replaceView(bachelorNoticeFragment)
                    }
                    2 -> {
                        replaceView(studentNoticeFragment)
                    }
                    3 -> {
                        replaceView(enrollNoticeFragment)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })




        // 웹 크롤링
        for (category in arrayOf(0,1,2,4)) { // 각 카테고리에 대해,
            for (page in 1..MAX_PAGE) { // 10 페이지 만큼,
                var thread: Thread
                if (page == 1) { // 각 공지사항 페이지의 1페이지만 주소가 다름
                    thread = Thread(UrlRun(PAGE1_FRONT_BASE_URL+"$category"+PAGE1_BACK_BASE_URL, page, applicationContext))
                } else { // 2페이지 이후로는 주소 형식이 같음 (페이지만 변화)
                    thread = Thread(UrlRun(AFTER_PAGE2_FRONT_BASE_URL+"$page"+ AFTER_PAGE2_BACK_BASE_URL+"$category",page,applicationContext))
                }
                // 스레드 실행
                thread.start()
                // 웹 크롤링 스레드가 끝날 때까지 메인 스레드 대기
                thread.join()
            }
        }
        Log.d("NoticeActivity/OnCreate", "웹 크롤링 완료")

    }

    // 탭 레이아웃 프래그먼트 교체 메서드
    private fun replaceView(tab: Fragment){
        var selectedFragment: Fragment? = null
        selectedFragment = tab
        selectedFragment?.let{
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, it).commit()
        }
    }


    // 웹 크롤링 스레드 클래스
    class UrlRun(var url: String, var pages: Int, var context: Context): Runnable{
        // TODO: 아이템 양식 정의
        // lateinit var items: Items
        @Synchronized
        override fun run() {
            try{
                // html 문서 가져오기
                var noticeHtml = Jsoup.connect(url).get()
                // 공지사항 아이템들 가져오기
                var items = noticeHtml.select(ITEM_ROUTE)
                // TODO: 가져온 아이템들을 양식에 맞게 출력(저장?)

            }
            catch(e: Exception){
                Log.e("NoticeActivity/UrlRun", e.toString())
            }
        }

    }
}