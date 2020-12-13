## Spring security
- Spring security는 DispatcherServlet 앞단에 Filter를 등록해 요청을 가로챔.
- 클라이언트에게 리소스 접근 권한이 없을 경우엔 인증화면으로 리다이렉트 함

## Filter
- Spring security는 기능별 필터의 집합인 필터체인을 가지고 있다.
- 필터의 처리 순서가 중요
- 클라이언트에 리소스 접근 권한이 없을 경우 인증화면으로 리다이렉트 하는 필터는 **UsernamePasswordAuthenticationFilter**
- Rest api의 경우 따로 로그인 폼이 없기 때문에 인증 권한이 없는 오류 Json을 **UsernamePasswordAuthenticationFilter** 전에 처리하여야 함

### Filter chain
1. ChannelProcessingFilter
2. SecurityContextPersistenceFilter
3. ConcurrentSessionFilter
4. HeaderWriterFilter
5. CsrfFilter
6. LogoutFilter
7. X509AuthenticationFilter
8. AbstractPreAuthenticatedProcessingFilter
9. CasAuthenticationFilter
10. UsernamePasswordAuthenticationFilter
11. BasicAuthenticationFilter
12. SecurityContextHolderAwareRequestFilter
13. JaasApiIntegrationFilter
14. RememberMeAuthenticationFilter
15. AnonymousAuthenticationFilter
16. SessionManagementFilter
17. ExceptionTranslationFilter
18. FilterSecurityInterceptor
19. SwitchUserFilter
