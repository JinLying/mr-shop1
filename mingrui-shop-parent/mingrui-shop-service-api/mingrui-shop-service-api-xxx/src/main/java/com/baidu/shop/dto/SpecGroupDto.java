package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDto;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MrOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName SpecGroupDto
 * @Description: SpecGroupDto
 * @Author jinluying
 * @create: 2020-09-03 11:41
 * @Version V1.0
 **/
@ApiModel("规格组数据传输DTO")
@Data
public class SpecGroupDto extends BaseDto {

    @ApiModelProperty(value  ="主键" ,example = "1")
    @NotNull(message = "主键能不能为空",groups = {MrOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "类型id",example = "1")
    @NotNull(message = "类型id不能为空",groups = {MrOperation.Add.class})
    private Integer cid;

    @ApiModelProperty(value = "规格组名称")
    @NotEmpty(message = "规格组名称",groups = {MrOperation.Add.class})
    private String name;

    @ApiModelProperty(hidden = true)
    private List<SpecParamEntity> specParams;
}
