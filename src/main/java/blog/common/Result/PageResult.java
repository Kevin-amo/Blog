package blog.common.Result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author admin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> records;

}
