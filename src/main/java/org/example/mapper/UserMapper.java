package org.example.mapper;

import org.example.dto.request.user.RegisterRequest;
import org.example.entity.User;
import org.example.mapper.utils.UserMapperUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                UserMapperUtil.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", qualifiedByName = {"UserMapperUtil", "getEncodedPassword"}, source = "password")
    User fromRegisterRequest(RegisterRequest registerRequest);

}
