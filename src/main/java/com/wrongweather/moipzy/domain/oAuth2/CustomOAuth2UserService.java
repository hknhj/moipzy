package com.wrongweather.moipzy.domain.oAuth2;

import com.wrongweather.moipzy.domain.temperature.TemperatureRange;
import com.wrongweather.moipzy.domain.temperature.service.TemperatureService;
import com.wrongweather.moipzy.domain.users.User;
import com.wrongweather.moipzy.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
       OAuth2User oAuth2User = super.loadUser(userRequest);
       Map<String, Object> attributes = oAuth2User.getAttributes();

        String registartionId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

//        System.out.println("getClientRegistration: "+userRequest.getClientRegistration());
//        System.out.println("getAttributes: "+super.loadUser(userRequest).getAttributes());
        log.info("getClientRegistrationId: {}", registartionId);
        log.info("userNameAttributeName: {}", userNameAttributeName);

        String email = oAuth2User.getAttribute("email");
//        System.out.println(email);
        log.info("email={}",email);

        // DB 조회한 결과 user가 있으면 넘어가고, 없으면 user 등록
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String name = oAuth2User.getAttribute("name");
            //System.out.println("유저 등록 안되어있으니까 등록");
            log.info("유저 등록 안되어있으니까 등록");

            return userRepository.save(User.builder() //비밀번호 없이 생성
                    .email(email)
                    .username(name)
                    .password(null)
                    .build());
        });

        log.debug("유저 조회 완료");
        return oAuth2User;
    }
}
