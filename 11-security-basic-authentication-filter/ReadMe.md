# BasicAuthenticationFilter

> ### BasicAuthenticationFilter

- Basic 인증을 처리한다.
  - HTTPS 프로토콜에서만 제한적으로 사용해야 한다.
  - HTTP 요청 헤더에 username과 password를 Base64 인코딩하여 포함한다.
  - Form 인증과 동일하게 `UsernamePasswordAuthenticationToken`을 사용한다.
  - httpBasic() 메서드를 호출하여 활성화 시킨다.
    - (default : 비활성화)