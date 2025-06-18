#DB #SQL 
## 高级SQL

### 使用程序设计语言访问SQL
**动态SQL**

JDBC

步骤：
- 连接到数据库 
```java
Connection conn = DriverManager.getConnection(URL,USER,PSW);
```
- 传递和使用sql语句
  使用Statement类 
```java
Statement stmt = conn.createStatement();

//Statement有executeQuery()方法----用于查询
//和executeUpdate()方法----用于增删改和创建表

//对于executeUpdate()方法：
String sql = "CREATE TABLE employee (id INT," +  
        " name VARCHAR(20) NOT NULL, " +  
        " age INT NOT NULL, " +  
        " address VARCHAR(50), " +  
        " salary REAL, " +  
        "PRIMARY KEY (id))";  

stmt.executeUpdate(sql);//直接把那个sql语句写进去。。。

//对于executeUpdate()方法
ResultSet rs = stmt.executeQuery("select * from employee");  //结果集
  
while (rs.next()){  //next方法用于测试结果集中是否存在一个未提取的元组，若有则提取。
    int id = rs.getInt("id");  //getXXX方法获取元素
    String name = rs.getString("name");  
    int age = rs.getInt("age");  
    String address = rs.getString("address");  
    int salary = rs.getInt("salary");  
  
    System.out.println("ID: " + id +  
            ", Name: " + name +  
            ", Age: " + age +  
            ", Address: " + address +  
            ", Salary: " + salary);  //输出
}

ResultSet有一个isBeforeFirst()方法，用于判断结果集是否有内容
if (!rs.isBeforeFirst()) {//如果找不到对应的学生  
    System.out.println("无相关学生，请重新输入。");  
    return false;
    }

```

**预备语句**
可以创建一条预备语句，使用？来代替某些值，此后会指明
```java
PreparedStatement pst = c.prepareStatement("INSERT INTO GPA VALUES (?,?)");  
for (int i = 0; i < 12; i++) {  
    pst.setString(1, strArray[i]);  //将strArray作为第一个参数  
    pst.setBigDecimal(2, BigDecimal.valueOf(doubleArray[i]));
    pst.addBatch();//添加批处理（但是不立刻执行）  
}  
pst.executeBatch();//执行批处理

String sql = "SELECT ID,name,dept_name,tot_cred FROM student WHERE ID = ?";  
try {  
    PreparedStatement pst = connection.prepareStatement(sql);  
    pst.setInt(1,Id);  
    ResultSet rs = pst.executeQuery();  
  
    if (!rs.isBeforeFirst()) {  
        System.out.println("无该学生，请重试。");  
        return false;//如果找不到对应的学生  
    }else {  
        while (rs.next()){  //遍历结果集  
            String id = rs.getString("ID");  
            String name = rs.getString("name");  
            String deptName = rs.getString("dept_name");  
            int totCred = rs.getInt("tot_cred");  
            System.out.println("ID: " + id +  
                    ", Name: " + name +  
                    ", Dept_name: " + deptName +  
                    ", Tot_cred: " + totCred );  
        }//这里找到了对应学号的学生  
        
    }
}catch (SQLException throwables) {  
    System.out.println("数据库查询出错："+throwables.getMessage());  
}

```

**SQL注入**
使用‘，“恶意破坏数据库

**可调用语句**
CallableStatement接口，允许调用SQL的存储过程和函数

**元数据特性**
ResultSet接口有一个getMetaData的方法，获取一个ResultMetaData的对象，可以获取列数，列的名称，或者指定列的类型等。

```java
ResultMetaData rsmd = rs.getMetaData();
for(int i =1;i<rsmd.getColumnCount();i++){
	System.out.println(rsmd.getColumnName(i));
	System.out.println(rsmd.getColumnTypeName(i));
}
```
Connection接口有一个getMetaData()方法，返回一个DatabaseMetaData对象，进一步有大量的方法来获取数据库的元数据。（具体请自己查询）
下面列出几个例子：
getColumns() 
getTable()
getPrimaryKeys()
getCrossReference()等等

**其他特性**
看书。

**Python访问数据库**

**ODBC开放数据库连接**

**嵌入式SQL**
SQL标准定义了将SQL嵌入各种程序设计语言的方法，嵌入了SQL的语言叫做宿主语言。
可以用来访问和更新存储在数据库中的数据。嵌入式SQL语言在编译之前必须先由特殊的预处理器进行处理1，该预处理器将嵌入的SQL请求替换为宿主语言的声明和过程调用，由宿主语言的编译器进行编译。
使用EXEC SQL语言
```sql
EXEC SQL （嵌入式SQL语句）
```

### 函数和过程

**定义**
create function name(attribute_name  tpye)
  return type
  begin
  ...
  return xxx；
  ...
  end

表函数：以表为返回的结果，当引用函数的参数时，需要加上函数名作为前缀

也可以写作一个过程：
create procedure dept_count (in dept_name varchar(20),out d_count integer)
	begin
		select count( dept_name) into d_count
		from instructor
		where instructor.dept_name = dept_count.dept_name
	end
使用call来调用过程

**用于过程和函数的语言结构**
PSM持久存储模块
declare 声明变量  declare dept_count integer
set 可以进行赋值
begin...end之间可以包含多个语句，并且可以声明局部变量
while,repeat,for,if,case语句
```sql
while 布尔表达式 do
	语句序列;
end while


repeat
    语句序列
until 布尔表达式
end repeat;


declare n integer default 0 --缺省值
for r as --每一次的循环结果都在r上
	select budget from department
	where dept_name = 'Music'
do
	set n = n - r.budget
end for

leave（break）
iterate（continue）


if 布尔
  then 语句
elseif 布尔
  then 语句
else 布尔
  then 语句
end if

case语句
```

异常处理
**declare** out_of_classroom_seats **condition**
**declare exit handler for** out_of-classroom_seats
begin
...
end

外部语言
sql允许程序设计语言来定义函数

### 触发器

[触发器](https://blog.csdn.net/weixin_53216033/article/details/142874458?fromshare=blogdetail&sharetype=blogdetail&sharerId=142874458&sharerefer=PC&sharesource=2301_79877217&sharefrom=from_link)
trigger 时作为对数据库修改的连带效果而由系统自动执行的语句
- 指明什么时候执行触发器。拆分为引起触发器被检测的一个事件和触发器继续执行所必须满足的一个事件
- 指明触发器执行时所采取的动作

create trigger timeslot_check after insert on section
refererncing new row as nrow    --过渡变量
for each row


### 递归查询
接下来看吧

### 高级聚集特性
#### 排名
[关于四大排名函数](https://blog.csdn.net/baidu_41797613/article/details/120449727?fromshare=blogdetail&sharetype=blogdetail&sharerId=120449727&sharerefer=PC&sharesource=2301_79877217&sharefrom=from_link)

order by

rank函数：对所有在orderby属性上相同的元组赐予相同的名词，112（这样的）
dense_rank：不产生空挡排名，112这样的

空值优先null first
空值最后null last
select ID, rank() over (order by GPAdesc nulls last) as sJank 
from student_grades;

limit子句

#### 分窗
窗口查询在一定范围内的元组上计算聚集函数。

```sql
SELECT 
    year,
    AVG(num_credits) OVER (ORDER BY year ROWS BETWEEN 3 PRECEDING AND CURRENT ROW) AS avg_totalcredits
```

- `year`：这是 `credits_table` 表中的一个字段，表示年份。查询结果中会包含每一行的 `year` 值。
- `AVG(num_credits) OVER (...)`：这是一个窗口函数的使用。
    - `AVG(num_credits)`：是一个聚合函数，用于计算 `num_credits` 字段的平均值。
    - `OVER` 子句：用于定义一个窗口，窗口函数会在这个窗口内进行计算。
        - `ORDER BY year`：表示按照 `year` 字段对结果集进行排序。窗口函数会根据这个排序顺序来确定每一行的前 3 行和当前行。
        - `ROWS BETWEEN 3 PRECEDING AND CURRENT ROW`：定义了窗口的范围。它表示窗口包含当前行以及当前行之前的 3 行。也就是说，对于每一行，窗口函数会计算当前行和前 3 行的 `num_credits` 的平均值。
    - `AS avg_totalcredits`：将计算得到的移动平均值命名为 `avg_totalcredits`，这个名称会作为结果集中的列名。

```sql
FROM 
    credits_table;
```

#### 旋转

#### 上卷和立方体
