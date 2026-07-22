package com.example.seckillzc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckillzc.entity.User;
import com.example.seckillzc.mapper.UserMapper;
import com.example.seckillzc.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
