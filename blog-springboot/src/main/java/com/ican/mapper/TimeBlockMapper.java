package com.ican.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ican.entity.TimeBlock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * TimeBlockMapper
 *
 * @author Aric
 */
@Repository
public interface TimeBlockMapper extends BaseMapper<TimeBlock> {

    @Select("SELECT DISTINCT name, category, color FROM t_time_block WHERE user_id = #{userId} ORDER BY name")
    List<Map<String, Object>> selectDistinctEvents(@Param("userId") Integer userId);

    @Select("SELECT category, SUM( " +
            "  TIMESTAMPDIFF(MINUTE, " +
            "    CONCAT(block_date, ' ', start_time, ':00'), " +
            "    CONCAT(block_date, ' ', end_time, ':00') " +
            "  ) " +
            ") AS total_minutes " +
            "FROM t_time_block " +
            "WHERE user_id = #{userId} AND block_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY category ORDER BY total_minutes DESC")
    List<Map<String, Object>> selectCategoryStats(
            @Param("userId") Integer userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
}