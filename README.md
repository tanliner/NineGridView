# 参考项目
这个项目 [jeasonlzy/NineGridView](https://github.com/jeasonlzy/NineGridView) 似乎停更了 

以下是对原库的修改 
1. 测量和布局的地方有修改，增加容错判断
2. `setAdapter` 当设置数据源为空的时候，可将自己隐藏，朋友圈可以不带图片分享
3. `columnCount = 1` 图片数量为1时，9宫格展示的图片按照微信的缩放规则去展示
按照宽高比分类
```
小于 1:3
1:3 到 1:1
1:1 到 3:1
超过3:1
```