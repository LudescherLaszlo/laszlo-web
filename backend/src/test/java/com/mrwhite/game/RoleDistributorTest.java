package com.mrwhite.game;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RoleDistributorTest {
    @Test void followsTheSpecificationTable() {
        int[][] expected={{3,0,1},{4,1,1},{6,1,1},{7,2,1},{9,2,1},{10,2,2},{12,2,2},
                {13,3,2},{15,3,2},{16,3,3},{18,3,3},{19,4,3},{21,4,3},{22,4,4},{24,4,4},{25,5,4},{27,5,4},{28,5,5},{30,5,5}};
        for(int[] row:expected){RoleDistribution d=RoleDistributor.distribution(row[0]);assertThat(d.undercovers()).isEqualTo(row[1]);assertThat(d.mrWhites()).isEqualTo(row[2]);}
    }
}
