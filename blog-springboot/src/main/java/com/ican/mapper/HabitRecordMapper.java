package com.ican.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ican.entity.HabitRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HabitRecordMapper extends BaseMapper<HabitRecord> {

    List<HabitRecord> selectRecordsByDateRange(@Param("userId") Integer userId,
                                               @Param("habitId") Integer habitId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    List<Map<String, Object>> selectDailyStats(@Param("userId") Integer userId,
                                                @Param("habitId") Integer habitId,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);
}
