package org.campusmarket.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.campusmarket.exchange.entity.CaptchaRecord;

// 验证码记录 Mapper 接口
@Mapper
public interface CaptchaRecordMapper extends BaseMapper<CaptchaRecord> {
    
    // 根据验证码ID查询验证码记录
    @Select("SELECT * FROM captcha_record WHERE captcha_id = #{captchaId}")
    CaptchaRecord selectByCaptchaId(@Param("captchaId") String captchaId);
} 