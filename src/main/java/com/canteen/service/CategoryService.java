package com.canteen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canteen.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}
