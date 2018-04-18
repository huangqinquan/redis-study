#Redis基本数据类型
1. 字符串
2. Hash
3. List
4. Set
5. Sort-set

总的看下来，jedis的api命名具有很大的信息量
 
_ex结尾_:expire一般是带有保存时间的参数，如setex  
_nx结尾_:Not eXists，如果不存在才创建，如setnx  
_m开头_ :同时设置多个key的，multi，如mset  
_decr和incr开头_:分别是自减和自增  

