package com.example.oauth2.security;

import static com.example.oauth2.security.SocialType.FACEBOOK;
import static com.example.oauth2.security.SocialType.GOOGLE;
import static com.example.oauth2.security.SocialType.KAKAO;

import com.example.oauth2.security.CustomOAuth2Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;



import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http.authorizeRequests()
	      .antMatchers("/", "/oauth2/**", "/login/**", "/css/**",
	    		  "/images/**", "/js/**", "/console/**")
	      .permitAll()
          .antMatchers("/facebook").hasAuthority(FACEBOOK.getRoleType())
          .antMatchers("/google").hasAuthority(GOOGLE.getRoleType())
          .antMatchers("/kakao").hasAuthority(KAKAO.getRoleType())
          .anyRequest().authenticated()
          .and()
          .oauth2Login()
          .defaultSuccessUrl("/loginSuccess")
          .failureUrl("/loginFailure")
          .and()
          .exceptionHandling()
          .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
	  }
	  
	  
	    @Bean  // 카카오
	    public ClientRegistrationRepository clientRegistrationRepository(
	    		OAuth2ClientProperties oAuth2ClientProperties,
	    		@Value("${custom.oauth2.kakao.client-id}") String kakaoClientId,
	    		@Value("${custom.oauth2.kakao.client-secret}") String kakaoClientSecret) {
	        List<ClientRegistration> registrations = oAuth2ClientProperties.
	        		getRegistration().keySet().stream()
	                .map(client -> getRegistration(oAuth2ClientProperties, client))
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());

	        registrations.add(CustomOAuth2Provider.KAKAO.getBuilder("kakao")
	                .clientId(kakaoClientId)
	                .clientSecret("test") //필요없는 값인데 null이면 실행이 안되도록 설정되어 있음
	                .jwkSetUri("test") //필요없는 값인데 null이면 실행이 안되도록 설정되어 있음
	                .build());

	        return new InMemoryClientRegistrationRepository(registrations);
	    }
	    
	    
	    private ClientRegistration getRegistration(OAuth2ClientProperties clientProperties, String client) {
	        if ("google".equals(client)) {
	            OAuth2ClientProperties.Registration registration = clientProperties.getRegistration().get("google");
	            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
	                    .clientId(registration.getClientId())
	                    .clientSecret(registration.getClientSecret())
	                    .scope("email", "profile")
	                    .build();
	        }
	        
	        if ("facebook".equals(client)) {
	            OAuth2ClientProperties.Registration registration = clientProperties.getRegistration().get("facebook");
	            return CommonOAuth2Provider.FACEBOOK.getBuilder(client)
	                    .clientId(registration.getClientId())
	                    .clientSecret(registration.getClientSecret())
	                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,link")
	                    .scope("email")
	                    .build();
	        }
	        return null;
	    }
}
