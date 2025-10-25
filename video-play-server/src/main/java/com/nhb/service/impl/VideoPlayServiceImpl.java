package com.nhb.service.impl;

import com.nhb.DAO.VideoPlayDAO;
import com.nhb.service.VideoPlayService;
import com.nhb.util.MinIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoPlayServiceImpl implements VideoPlayService {
    @Autowired
    private VideoPlayDAO videoPlayDAO;
    @Autowired
    private MinIOUtil minIOUtil;

}
