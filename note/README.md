# 스프링부트 IoC 컨테이너 클론 — 학습 완주 정리

밑바닥부터 스프링의 IoC/DI 컨테이너를 직접 구현하며 배운 3단계 성장형 학습 기록.
출처: slog.gg/p/14161 · 스타터: jhs512/p-14161-{1,2,3}

## 강별 기록
- [1강.md](1강.md) — 하드코딩으로 빈 생성 + 싱글톤 + DI (t1~t6)
- [2강.md](2강.md) — 리플렉션 컴포넌트 스캔 + 자동 DI (하드코딩 제거)
- [3강.md](3강.md) — @Configuration/@Bean 메서드로 외부 라이브러리 객체 조립 (t7·t8)

## 한눈에 보는 진화

| | 1강 (v1) | 2강 (v2) | 3강 (v3) |
|---|---|---|---|
| 빈 등록 | `switch` 하드코딩 | `@Component` 스캔 | + `@Bean` 메서드 스캔 |
| 객체 생성 | 직접 `new` | 생성자 `newInstance` | + 메서드 `invoke` |
| DI(의존성 주입) | 손으로 `genBean` 호출 | 생성자 파라미터를 **타입**으로 재귀 조달 | + @Bean 파라미터를 **이름**으로 조달 |
| 외부 라이브러리 | - | @Component 불가 | @Bean으로 조립 가능 |
| 핵심 도구 | `Map`, 싱글톤 | `Reflections`, 리플렉션 | `Method.invoke`, `-parameters` |

## 전 과정을 관통하는 3가지 원리
1. **싱글톤 창고**: `Map<String,Object> beans` + `containsKey` 검사. 1~3강 내내 동일.
2. **재귀 조립**: 빈을 만들 때 필요한 재료를 `genBean`으로 다시 요청. 의존성 그래프를 자동 조립.
3. **리플렉션**: 런타임에 클래스/생성자/메서드/파라미터를 읽어 코드를 대신 실행. 스프링 "마법"의 정체.

## 최종 genBean 흐름
```
genBean(name):
  1) beans에 있으면 → 반환 (싱글톤)
  2) 없으면 생성:
     - name이 @Bean 메서드면 → 설정 객체 조달 → 파라미터를 "이름"으로 재귀 조달 → method.invoke
     - name이 @Component 클래스면 → 생성자 파라미터를 "타입"으로 재귀 조달 → constructor.newInstance
  3) beans에 저장 → 반환
```

## 환경
- Java 25 (Temurin), Gradle 9.3
- 테스트: `./gradlew test --tests "com.ll.framework.ioc.ApplicationContextTest"`
- 원격: github.com/jomin4/prgrms-Springboot_loc_container

*프로젝트 완주 — 2026-08-18. 시각화 자료(SVG)는 채팅 인라인으로 진행, 추후 최종 정리 예정.*
