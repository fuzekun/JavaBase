package threadBase.JUC.model;

import lombok.Data;

/**
 * @author: Zekun Fu
 * @date: 2022/6/24 11:49
 * @Description:
 * 为了测试缓存，需要用到数据库
 * 为了使用数据库，使用mybatis
 * 为了测试mybatis，使用创建实体类
 * 为了实体类，创建了这个东西
 *
 */
@Data
public class Student {
    String name;
    int id;
    String tel;



}
