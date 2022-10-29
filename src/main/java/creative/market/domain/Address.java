package creative.market.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Address {

    private String jibun;

    private String road;

    private int zipcode;

    private String detailAddress;

    @Builder
    public Address(String jibun, String road, int zipcode, String detailAddress) {
        this.jibun = jibun;
        this.road = road;
        this.zipcode = zipcode;
        this.detailAddress = detailAddress;
    }
}
