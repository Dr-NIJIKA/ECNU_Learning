#DB #关系 
# 复杂数据类型
### 半结构化数据
#### 概述
1. 灵活模式
	宽列：每个元组有不同的属性集
	稀疏列：每个元组只需要使用它要的属性，剩下为null
2. 多值数据类型
	允许将集合、多重集合或者数组作为属性值
	或者储存MAP（K-V对）
	时间类型
	NFNF：非一范式
3. 嵌套数据类型。
	允许对属性进行结构化，对ER模型中的复合属性直接建模
	两种数据类型：JSON和XML
4. 知识表示
	RDF
#### JSON
JavaScript对象表示法
支持整数，浮点数，字符串等基础数据类型，以及数组和对象（属性，值）对的集合
{
“XX”:"xxxx"
}
JSON是冗长的，相同的数据会占用更多的储存空间。
解析文本以减速所需字段会占用大量CPU资源，压缩表示形式无须解析即可
使用BSON，用二进制形式来存储JSON数据
在SQL中
- JSON数据可以被储存为JSON数据类型
- SQL查询可以生成JSON数据
- 可以从JSON对象中提取数据
#### XML
使用<>括起来的标签表示，以在文本表示中标记信息。
通过将关系名和属性名指定为标签，标签用于表示关系数据
```xml
<course> 
	<course_id> CS-101</course_id>
	<title> Intro. to Computer Science </title> 
	<depLname> Comp. Sci. </depLname> 
	<credits> 4 </credits> 
</course>
```
每一条数据都需要加tag
冗余感
SQL对于XML的处理
- XML数据可以储存为XML数据类型
- SQL查询可以从关系数据生成XML数据
- SQL查询允许从XML数据类型中提取数据

#### RDF（知识表示）和知识图谱
资源描述框架。基于E-R模型的数据表示标准
1. 三元组表示
	- （ID，属性名，属性值）
	- （ID1，联系名，ID2）
	- ID是实体的标识，实体是RDF的**资源**
	- RDF支支持二元联系
	三元组具有（主题，谓词，对象）的结构

2. RDF的图表示
	- 对象使用椭圆表示
	- 属性值使用矩形表示
	![[Pasted image 20250426201739.png]]
3. SPARQL
	一种为查询RDF数据而设计的查询语言，基于三元组模式
    -  ?cid title "Intro. to Computer Science"	
    - ？cid是变量，可以在三元组中共享
    - 匹配谓词为title并且对象为“xxx”的三元组
    - 共享变量旨在确保元组之间的连接

4. n元连接的表示
	- 聚集作为实体考虑（实体化）
	- 加入第四个属性：上下文，存储四元组

### 面向对象
**对象-关系数据模型**
提供了丰富的系统类型拓展关系数据类型
使用三种方法将面向对象特性和数据库系统集成到一起
- 构建对象-关系数据库
- 使用关系-对象映射来进行数据转化
- 构建面向对象的数据库系统

1. 对象-关系数据库系统
SQL对象拓展允许创建结构化的用户自定义类型
```sql
create type Person 
	(ID varchar(20) primary key, 
	name varchar(20), 
	address varchar(20)) ref from(JD); 

create table people of Person;
```
类型继承
	使用继承来定义新的类型
```sql
	create type Student
	 under Person (degree varchar(20))
```
表继承
	允许一个表声明为另一个表的子表，对应特化/概化的概念。
	不同的数据库系统具有不同的方式。
引用类型
```sql
create type Person 
	(ID varchar(20) primary key, 
	name varchar(20), 
	address varchar(20)) ref from(JD); 

create table people of Person;
```
方法是使用ref from子句，定义数据类型的时候储存对另一个表中对象类型的引用
scope子句实现外码定义
```sql
create type Department
( 
	dept_name varchar(20), 
	head ref(Person) scope people
); 
create table departments of Department
```


2. 对象-关系映射
   允许程序员定义数据库关系中的元组和变成语言中的对象之间的映射


### 文本数据

#### 关键词查询
常用keyword集合来描述
关键词查询会检索出那些关键字集合包含查询中的所有关键字文档
#### 相关性排名
1. 使用TF-IDF的排名
- 术语是指文档中出现的关键字
- 需要解决问题是：给你一个属于t，计算和特定文档d的相关性**术语频率**
- ==TF(d, t) = log (1+ n(d, t) /n(d))==  n(d)表示d中出现术语的次数，n(t，d)表示文档d中出现属于t的次数    



### 空间数据
