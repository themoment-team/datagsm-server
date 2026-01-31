# DataGSM

> 광주소프트웨어마이스터고등학교 OAuth & OpenAPI 서비스

DataGSM은 광주소프트웨어마이스터고등학교의 학생, 동아리, 급식, 학사일정 등의 정보를 제공하는 **OAuth2 인증 서버**와 **OpenAPI 리소스 서버**입니다.

## 모듈 구조

| 모듈                              | 역할                                     | 포트   |
|---------------------------------|----------------------------------------|------|
| **datagsm-common**              | 공통 Entity, DTO, Repository, Health API | -    |
| **datagsm-oauth-authorization** | OAuth2 인증 서버                           | 8081 |
| **datagsm-oauth-userinfo**      | OAuth2 UserInfo API                    | 8083 |
| **datagsm-openapi**             | 리소스 API (학생, 동아리, NEIS)                | 8082 |
| **datagsm-web**                 | 관리자 웹 API                              | 8080 |

## 문서

- **공식 기술 문서**: https://datagsm-front-client.vercel.app/docs
- **기여 가이드**: [CONTRIBUTING.md](./CONTRIBUTING.md)

## 기여하기

프로젝트에 기여하고 싶으시다면 [기여 가이드](./CONTRIBUTING.md)를 참고해주세요.

## 라이선스

이 프로젝트는 [MIT 라이선스](./LICENSE)를 따릅니다.

## 문의

- **이메일**: datagsm.oauth@gmail.com
- **이슈**: [GitHub Issues](https://github.com/themoment-team/datagsm-server/issues)

---

Made with ❤️ by [themoment-team](https://github.com/themoment-team)