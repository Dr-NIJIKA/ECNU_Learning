#DB #SQL
## 中级SQL
### 连接表达式
nature join（去重的等值连接）
 
 join...using（某个属性）

join...on (一个谓词)

外连接（处理悬浮元组）
没有与一个元素相对应的元素，用null代替
left outer join
right outer join
full outer join

### 视图View
格式：
```sql
create view v(A1,A2) as
	<查询表达式>;

create view faculty as 
select ID, name, depLname 
from instructor;
```
- 视图一旦创建，在被显式删除之前就是一直可用的，with定义的命名子查询是本地可用的
- 一旦定义了视图，就可以使用视图名来代指生成的虚拟关系。
- 可以显式指定视图的属性名(上图A1A2)
- 我们定义视图时，存储的不是查询的结果，存储他本身的定义。
- 视图可用于另一个视图中

**物化视图**
定义视图的关系发生变化，视图也跟着修改保持改变
**视图维护**
保持物化视图一直在最新状态的过程
**视图可更新**
- from子句只有一个数据库关系
- select子句只包含属性名，不含任何表达式或者聚集或者distinct
- not null
- 不含有groupby和having子句

### 事务transcations
由查询和更新语句的序列构成
SQL规定，当一条SQL语句被执行的实行就隐式的开始了一个事务
结束：
- commit work 事务执行的更新在数据库中成为永久的；
- rollback work 撤销执行的更新，回到最开始的状态。

原子性：一个事务或者在完成所有步骤之后提交其操作，或者不能完成所有操作后滚回，不可分割。

很多数据库支持关闭自动提交
或者使用begin automic ... end（具体情况具体分析）

### 完整性约束
保证授权用户对数据库所作的修改不会导致数据一致性的丢失，防止对数据库的意外破坏
（区别安全性约束）
单个关系上的约束：
- primary key
- not null
	是域约束的一个示例
- unique
	unique（A1，A2....）
- check（<谓词>）
	例如：check(semester in ('Fall','Sqring'))
	或：  check(id>0)
- 引用完整性
	foreign key（deptname）reference department
- 约束命名：
	alary numeric(8,2) , constraint minsalaf")'check(salary> 29000)
	alter table instructor drop constraint minsalary;
- 断言：
	create assertion  (name)  check  (predicate) 


### 数据类型和模式
- 日期和时间类型
	日期 ：年-（四位）月-日
	时间 ：时:分:秒 使用time(p)指定小数位数，使用time with timezone储存时区信息
	时间戳 ： date 和 time 的结合
	可使用extract（feild from d）提取域
	特殊函数：
		current_date
		localtime
		current_timestamp
		localtimestamp
	日期时间（datetime）
	SQL还提供了一种叫区间的数据类型，运行日期，时间等进行计算

- 类型转化和格式化函数
	可用cast（e as t）的表达式来将表达式e转化为类型t

	mysql使用了format函数

	处理null：
	使用coalesce（salary，0）,其限制是要求所有参数必须是相同的类型

- 缺省值
	SQL允许为属性指定缺省（default）值
```sql
creat table student(
	ID    varchar(5),
	name  varchar(20) not null,
	tot_cred numeric(3,0)default 0
)	;
```
这里tot_cred属性的缺省值被声明为0，其结果就是，如果插入元素没有给出totcred的值，那么取值就被置为0。

- 大数据对象
	clob字符数据
	blob二进制数据

- 用户自定义类型
	独特类型
	结构化数据类型
	
	提供了drop type和alter type子句来删除或者修稿之前创造过的类型
	域	create domain DDollars as numeric(12,2) not null
	域可用作属性类型
	
	关于独特类型：
```sql
create type Dollar as numeric(12,2) final;
creat table student(
	ID    varchar(5),
	name  varchar(20) not null,
	tot_cred numeric(3,0)default 0
	fee   Dollar
)	;
```

- 生成唯一码值
	number（5）
	ID number(5) ==generated always as identity== 在mysql内为auto_increment
	使用always时，必须避免insert为自动生成的码指定一个值

- create table拓展
	create table like

- 模式，目录，环境
三层体系结构：
目录：最顶层，包含模式
模式：关系，视图都包含在模式中   create schema/drop schema
使用**目录.模式.关系名称**来访问
SQL环境
### 索引
索引是一种数据结构，允许数据库系统高效的找到在关系中具有属性指定值的那些元组，而不扫描所有的元组.
索引的格式：
create index name on r(attribute);
如果要声明搜索码是一个候选码，就要添加unique
create unique index name on r(attribute);
撤销索引：
drop index name；

### 授权
授权包括：（每种类型的授权都是一种权限）
增    insert
删    delete
查    select
改    update
所有权限    all privilege（关系的创建者获得所有权限）

**使用grant语句来授予权限**
```sql
grant <权限列表>
on  <关系或视图>
to <用户或角色>
```
选择：用于读取关系中的元组
 grant select on department to Amit, Satoshi; 

更新：允许用户修改关系中的任意元组，也可以用在属性上
 grant update (budget) on department to Amit, Satoshi; # 在属性budget上

插入：插入元组或者属性列表

删除：删除元组

public 隐含对当前和未来所有用户的授权

**revoke来进行收权**
```sql
revoke <权限列表>
on  <关系或视图>
to <用户或角色>
```

**角色：** 
每个数据库用户授予一组他有权扮演的角色
```sql
create role dean; 
grant instructor to dean; --角色授权给角色
grant dean to Satoshi;--角色授权给用户
```
所以权限包括：
- 直接授予的权限
- 授予该对象权限的角色的所有权限
所以会出现角色链

**视图的授权**
函数和过程中的**执行**权限，以允许用户执行函数或者过程

**模式的授权**
基本的授权机制：只有模式的拥有者才能执行模式的任何修改

提供了一种引用权限：
允许用户在创建关系的时候声明外码
grant references (dept_name)on department to Maya ;
允许用户Maya创建这样的关系：能引用department的dept_name码作为外码

**权限的转移和收回**
在授权语句之后加上with grant option
```sql
grant select on department to Amit with grant option；
```
授权图
级联收权：从一个用户那里挥手权限可能会导致别的用户失去权限

可以通过restrict来防止级联收权
revoke grant option for select on department from Amit;

**行级授权**
