# HeaderWriterFilter

> ### HeaderWriterFilter

- 응답 헤더에 보안 관련 헤더 추가한다.
  - 관련 이슈에 대해 기본적인 방어 기능은 완벽하지는 않다.
  - 브라우저마다 다르게 동작할 수 있다.

> ### XContentTypeOptionsHeaderWriter

- MIME sniffing 공격 방어
  - 브라우저에서 `MIME sniffing`을 사용하여 `Request Content Type`을 추측할 수 있다.
    - XSS 공격에 악용될 수 있다.
  - 지정된 MIME 형식 이외의 다른 용도로 사용하는 것을 차단한다.

> ### XXssProtectionHeaderWriter

- 브라우저에 내장된 XSS(Cross-Site Scripting) 필터 활성화
  - XSS
    - 웹 상에서 가장 기초적인 취약점 공격 방법의 일종이다. 
      - 악의적인 사용자가 공격하려는 사이트에 스크립트를 넣는 기법이다.
  - 일반적으로 브라우저에는 XSS 공격을 방어하는 필터링 기능이 있다.
  - 해당 필터로 XSS 공격을 완벽하게 방어하지 못한다.

> ### CacheControlHeadersWriter

- 캐시를 사용하지 않도록 설정한다.
- 브라우저 캐시 설정에 따라 사용자가 인증 후 방문한 페이지를 로그아웃한 후 캐시된 페이지를 악의적인 사용자가 볼 수 있다.

> ### XFrameOptionsHeaderWriter

- `clickjacking` 공격 방어
- 웹 사용자가 자신이 클릭하고 있다는 인지하는 것과 다른 어떤 것을 클릭하게 속이는 악의적인 기법
- 보통 사용자의 인식 없이 실행될 수 있는 임베디드 코드, 스크립트 형태

> ### HstsHeaderWriter

- HTTP 대신 HTTPS만을 사용하여 통신해야함을 브라우저에 알린다.