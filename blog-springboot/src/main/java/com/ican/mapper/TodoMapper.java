package com.ican.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ican.entity.Todo;
import com.ican.model.vo.query.TodoQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoMapper extends BaseMapper<Todo> {

    List<Todo> selectTodoList(@Param("param") TodoQuery query, @Param("userId") Integer userId);

    Long selectTodoCount(@Param("param") TodoQuery query, @Param("userId") Integer userId);

    List<Todo> selectTodoByDateRange(@Param("userId") Integer userId,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
}
