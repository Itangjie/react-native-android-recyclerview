
# react-native-android-recyclerview

## Getting started

`$ npm install react-native-android-recyclerview --save`

### Mostly automatic installation(react-native < 0.60)

`$ react-native link rreact-native-android-recyclerview`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.rn.recycleview.RNRecycleviewPackage;` to the imports at the top of the file
  - Add `new RNRecycleviewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-recyclerview'
  	project(':react-native-android-recyclerview').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-recyclerview/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-android-recyclerview')
  	```


## Usage
```javascript
import {RecyclerView,DataSource} from 'react-native-android-recyclerview';

<RecyclerView
    ref={(component) => this._recycler = component}
    style={{ flex: 1 }}
    dataSource={dataSource}
    renderItem={(item,index)=>{
        return(
            <View/>
        )   
    }}
    windowSize={50}
    initialScrollIndex={0}
    inverted={false}
    column={1}
    onScrollBeginDrag={(event)=>{
        console.log('onScrollBeginDrag')
    }}
    onScroll={(event)=>{
        console.log('onScroll')
    }}
    onScrollEndDrag={(event)=>{
        console.log('onScrollEndDrag')
    }}
    onContentSizeChange={(event)=>{
        console.log('onContentSizeChange')
    }}
    onVisibleItemsChange={(event)=>{
        console.log('onVisibleItemsChange')
    }}
    ListHeaderComponent={(
        <View style={{ paddingTop: 15, backgroundColor: '#eee' }} />
    )}
    ListFooterComponent={(
        <View style={{ paddingTop: 15, backgroundColor: '#aaa'}} />
    )}
    ListEmptyComponent={(
        <View style={{ borderColor: '#e7e7e7', borderWidth: 1, margin: 10, padding: 20, }}>
            <Text style={{ fontSize: 15 }}>Empty Component</Text>
        </View>
    )}
    ItemSeparatorComponent={(
        <View style={{ borderBottomWidth: 1, borderColor: '#e7e7e7', marginHorizontal: 5, marginVertical: 10 }} />
    )} 
/>
```
## Example
Check [example](https://github.com/Itangjie/react-native-android-recyclerview/tree/master/example) in the  folder.

```bash
$ cd example
$ npm install
$ react-native run-android
```

## Other
Project fork's [react-native-recyclerview-list](https://github.com/godness84/react-native-recyclerview-list)

