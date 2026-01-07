package team.themoment.datagsm.authorization.global.thirdparty.feign.neis

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import team.themoment.datagsm.authorization.global.thirdparty.feign.config.FeignConfig
import team.themoment.datagsm.authorization.global.thirdparty.feign.neis.dto.NeisMealApiResponse
import team.themoment.datagsm.authorization.global.thirdparty.feign.neis.dto.NeisScheduleApiResponse

@FeignClient(
    name = "neis-api-client",
    url = "https://open.neis.go.kr/hub",
    configuration = [FeignConfig::class],
)
interface NeisApiClient {
    /**
     * 급식 식단 정보 조회
     *
     * @param key NEIS API 인증키
     * @param type 응답 데이터 타입 (json/xml)
     * @param pIndex 페이지 번호
     * @param pSize 페이지당 데이터 수
     * @param atptOfcdcScCode 시도교육청코드
     * @param sdSchulCode 표준학교코드
     * @param mlsvYmd 급식일자 (YYYYMMDD)
     * @param mlsvFromYmd 급식시작일자 (YYYYMMDD)
     * @param mlsvToYmd 급식종료일자 (YYYYMMDD)
     * @return 급식 식단 정보 응답
     */
    @GetMapping("/mealServiceDietInfo")
    fun getMealServiceDietInfo(
        @RequestParam("KEY") key: String,
        @RequestParam("Type") type: String = "json",
        @RequestParam("pIndex") pIndex: Int = 1,
        @RequestParam("pSize") pSize: Int = 100,
        @RequestParam("ATPT_OFCDC_SC_CODE") atptOfcdcScCode: String,
        @RequestParam("SD_SCHUL_CODE") sdSchulCode: String,
        @RequestParam(value = "MLSV_YMD", required = false) mlsvYmd: String? = null,
        @RequestParam(value = "MLSV_FROM_YMD", required = false) mlsvFromYmd: String? = null,
        @RequestParam(value = "MLSV_TO_YMD", required = false) mlsvToYmd: String? = null,
    ): NeisMealApiResponse

    /**
     * 학사일정 정보 조회
     *
     * @param key NEIS API 인증키
     * @param type 응답 데이터 타입 (json/xml)
     * @param pIndex 페이지 번호
     * @param pSize 페이지당 데이터 수
     * @param atptOfcdcScCode 시도교육청코드
     * @param sdSchulCode 표준학교코드
     * @param aa_ymd 학사일자 (YYYYMMDD)
     * @param aa_from_ymd 학사시작일자 (YYYYMMDD)
     * @param aa_to_ymd 학사종료일자 (YYYYMMDD)
     * @return 학사일정 정보 응답
     */
    @GetMapping("/SchoolSchedule")
    fun getSchoolSchedule(
        @RequestParam("KEY") key: String,
        @RequestParam("Type") type: String = "json",
        @RequestParam("pIndex") pIndex: Int = 1,
        @RequestParam("pSize") pSize: Int = 100,
        @RequestParam("ATPT_OFCDC_SC_CODE") atptOfcdcScCode: String,
        @RequestParam("SD_SCHUL_CODE") sdSchulCode: String,
        @RequestParam(value = "AA_YMD", required = false) aa_ymd: String? = null,
        @RequestParam(value = "AA_FROM_YMD", required = false) aa_from_ymd: String? = null,
        @RequestParam(value = "AA_TO_YMD", required = false) aa_to_ymd: String? = null,
    ): NeisScheduleApiResponse
}
