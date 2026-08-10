package com.gien.gits.ontology.port;

import com.gien.gits.ontology.KycGapProfile;

/**
 * KYC差距画像可写仓储端口
 */
public interface WritableKycGapProfileRepository extends KycGapProfileRepository {
    void save(KycGapProfile profile);
}
