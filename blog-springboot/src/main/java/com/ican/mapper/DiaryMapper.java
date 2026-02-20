package com.ican.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ican.entity.Diary;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryMapper extends BaseMapper<Diary> {

    Diary selectByUserAndDate(@Param("userId") Integer userId, @Param("diaryDate") String diaryDate);

    List<Diary> selectDiaryByDateRange(@Param("userId") Integer userId,
                                       @Param("startDate") String startDate,
                                       @Param("endDate") String endDate);
}
