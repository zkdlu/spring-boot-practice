참고 https://d2.naver.com/helloworld/24942

### OAuth2 
1. [Client] Social 로그인
2. [Social] 로그인 성공 시 authorization_code 반환
3. [Client] authorization_code로 access_token 요청
4. [Social] access_token 반환
5. [Client] access_token으로 서버에 로그인 요청
6. [Server] access_token으로 유저 정보 요청
7. [Social] 유저정보 반환 
8. [Server] 유저 인증 후 JWT 인증 토큰 발금
9. [Client] JWT 인증 토큰으로 api 요청
10. [Server] api 결과 반환

## 1. 카카오 로그인
### 카카오 개발자 센터 설정
1. 카카오 개발자 센터에 app 생성 (https://developers.kakao.com/)
2. 플랫폼 생성(web: http://localhost:8080)
3. Redirect URI 등록 (http://localhost:8080/social/login/kakao)
  > 카카오 로그인 연동 후 인증 성공시 Callback 받을 URL
### 로그인 페이지 및 콜백 페이지 연동
### accessToken으로 가입 및 로그인 처리
