package com.wrongweather.moipzy.domain.temperature;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class OuterTempRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int rangeId; //DB column 이름과 같아야됨

    @Column(nullable = true, length = 50)
    private String over28;

    @Column(nullable = true, length = 50)
    private String between27_24;

    @Column(nullable = true, length = 50)
    private String between23_20;

    @Column(nullable = true, length = 50)
    private String between19_17;

    @Column(nullable = true, length = 50)
    private String between16_14;

    @Column(nullable = true, length = 50)
    private String between13_11;

    @Column(nullable = true, length = 50)
    private String between10_8;

    @Column(nullable = true, length = 50)
    private String between7_5;

    @Column(nullable = true, length = 50)
    private String under4;

    @Builder
    public OuterTempRange(String over28, String between27_24, String between23_20, String between19_17, String between16_14, String between13_11, String between10_8, String between7_5, String under4) {
        this.over28 = over28;
        this.between27_24 = between27_24;
        this.between23_20 = between23_20;
        this.between19_17 = between19_17;
        this.between16_14 = between16_14;
        this.between13_11 = between13_11;
        this.between10_8 = between10_8;
        this.between7_5 = between7_5;
        this.under4 = under4;
    }
}
