package app.kaidoku.fancafe.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사이트 설정(key/value). 채널 ID 등 운영 데이터는 코드가 아니라 여기서(하드코딩 금지 룰). */
@Entity
@Table(name = "site_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteSetting {

    /** 씨미 채널 ID 설정 키. */
    public static final String KEY_SEEME_CHANNEL_ID = "seeme.channel_id";

    /** 배너 외부 링크 설정 키. 값은 URL 또는 빈값(null). */
    public static final String KEY_BANNER_YOUTUBE = "BANNER_YOUTUBE";
    public static final String KEY_BANNER_X = "BANNER_X";
    public static final String KEY_BANNER_SEEME = "BANNER_SEEME";
    public static final String KEY_BANNER_FANCIM = "BANNER_FANCIM";
    public static final String KEY_BANNER_FANCIMM = "BANNER_FANCIMM";

    @Id
    @Column(name = "setting_key", length = 100)
    private String key;

    @Column(name = "setting_value", length = 1000)
    private String value;

    public static SiteSetting of(String key, String value) {
        SiteSetting s = new SiteSetting();
        s.key = key;
        s.value = value;
        return s;
    }

    public void changeValue(String value) {
        this.value = value;
    }
}
