package team.themoment.datagsm.domain.club.service;

import org.jetbrains.annotations.NotNull;
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto;
import team.themoment.datagsm.domain.club.dto.response.ClubResDto;

public interface ModifyClubService {
    @NotNull
    ClubResDto execute(long clubId, @NotNull ClubReqDto reqDto);
}
