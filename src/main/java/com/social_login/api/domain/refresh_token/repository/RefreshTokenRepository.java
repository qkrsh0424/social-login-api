package com.social_login.api.domain.refresh_token.repository;

import java.util.Optional;
import java.util.UUID;

import com.social_login.api.domain.refresh_token.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {

    // 액세스토큰의 업데이트순 가장 최신 3개를 제외하고 모두 제거
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_token WHERE user_id=:userId AND cid NOT IN\n"
        + "(SELECT tmp.* FROM (SELECT rt2.cid FROM refresh_token AS rt2 WHERE rt2.user_id=:userId\n"
        + "ORDER BY rt2.updated_at DESC LIMIT :allowedAccessCount) AS tmp)", nativeQuery = true)
    public void deleteOldRefreshTokenForUser(String userId, Integer allowedAccessCount);

    Optional<RefreshTokenEntity> findById(UUID id);
}
