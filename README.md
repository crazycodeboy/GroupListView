# GroupListView简介 #
- GroupListView支持上拉加载下拉刷新，并且可以根据需要选择是否启用下拉刷新、上拉加载
- GroupListView支持列表项悬停效果    
 
## 运行效果图 ##
![运行效果图](https://github.com/crazycodeboy/GroupListView/blob/dev/raw/%E8%BF%90%E8%A1%8C%E6%95%88%E6%9E%9C%E5%9B%BE.gif?raw=true)


## PS. ##

>为了实现支付宝账单列表的悬停、上拉加载、下拉刷新的效果，我从Git上Fetch了pulltorefresh和PinnedSectionList两个开源项目，并对pulltorefresh进行修改实现上拉加载的效果，将修改后的pulltorefresh更名成了ListViewPlus，最后将PinnedSectionList的library依赖于ListViewPlus,就实现了带有悬停效果并且支持上拉加载、下拉刷新的GroupListView,现在将其开源出来供大家使用。

## 使用方法 ##
1.  GroupListView是基于ListView开发的一个控件，所以大家可以使用ListView的方式来使用它。
2.  ListViewPlus部分：关于ListViewPlus方面的功能大家可以参考[https://github.com/crazycodeboy/ListViewPlus](https://github.com/crazycodeboy/ListViewPlus "ListViewPlus")
3.  PinnedSectionList部分的使用方法如下：
- 数据适配器必须实现如下方法：

```java
@Override
public int getItemViewType(int position) {
	return items.get(position).getType();
}
@Override
public int getViewTypeCount() {
	return 2;
}
@Override
public boolean isItemViewTypePinned(int viewType) {
	return viewType==Item.SECTION;
}
```

4 .其它使用细节可以参照实例。
