테스트를 할 자바 클래스를 열고 Ctrl+Shift+T를 누르면 테스트 생성 메뉴가 열림

# Junit assert 메서드
- assertNotNull(obj)
- assertTrue(condition), assertFalse(condition)
- assertEquals(o1, o2), assertNotEquals(o1, o2) //값이 같은지
- assertSame(o1, o2) //객체가 같은지
- assertArrayEquals(arr1, arr2)
- asertThat(T, Matcher) // T: 비교대상, Matcher: 비교

# MockMvc 메서드
- perform
> - 주어진 url을 수행할 수 있는 환경 구성
> - GET, Post, Put, Delete등 처리 가능
> - header 셋팅, AcceptType 설정 가능
> - mockMvc.perform(post("/v1/signin").params(params)

- andDo
> - perform요청을 처리. andDo(print()) 하면 처리 결과를 console에서 확인 가능

- andExpect
> - 검증내용을 체크
> - andExpect(status().isOk())
> - 결과가 json인 경우, andExpect(jsonPath("$.success").value(true))

- andReturn
> - 테스트 완료 후 결과 객체를 가져온다.
> - MvcResult result = mockMvc.perform(post("/v1/signin").params(params)).andDo(print());

## JPA Test를 위한 @DataJpaTest
- 다른 컴포넌트들은 로드 하지 않고 @Entity만 읽어 Repository 내용을 테스트 할 수 있는 환경을 만들어 준다.
- 따로 @Transactional을 포함하고 있어 테스트가 완료되면 롤백이 된다.
```java
@RunWith(SpringRunner.class)
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    public void whenFindByUid_thenReturnUser() {
        String uid = "zkdlu";
        String name = "이건";
        // given
        userRepository.save(User.builder()
                .uid(uid)
                .password(passwordEncoder.encode("1234"))
                .name(name)
                .roles(Collections.singletonList("ROLE_USER"))
                .build());
        // when
        Optional<User> user = userRepository.findByUid(uid);
        // then
        assertNotNull(user);// user객체가 null이 아닌지 체크
        assertTrue(user.isPresent()); // user객체가 존재여부 true/false 체크
        assertEquals(user.get().getName(), name); // user객체의 name과 name변수 값이 같은지 체크
        assertThat(user.get().getName(), is(name)); // user객체의 name과 name변수 값이 같은지 체크
    }
}
```

## Spring Boot Test
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SignControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() throws Exception {
        userRepository.save(
                User.builder()
                        .uid("zkldu")
                        .name("이건")
                        .password(passwordEncoder.encode("1234"))
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build());
    }

    @Test
    public void signin() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "zkdlu");
        params.add("password", "1234");
        mockMvc.perform(post("/v2/signin").params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void signup() throws Exception {
        long epochTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "zkdlu_" + epochTime);
        params.add("password", "12345");
        params.add("name", "zkdlu_" + epochTime);
        mockMvc.perform(post("/v2/signup").params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());
    }
}
```
