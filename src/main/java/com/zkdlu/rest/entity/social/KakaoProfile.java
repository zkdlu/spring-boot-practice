package com.zkdlu.rest.entity.social;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoProfile {
    private long id;
    private Properties properties;

    private static class Properties {
        private String nickname;
        private String thumbnail_image;
        private String profile_image;
    }
}
