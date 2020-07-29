
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
import {RecyclerView,DataSource} from 'react-native-android-recycleview';

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
/>
```
## Example
Check [example](https://github.com/Itangjie/react-native-android-recyclerview/example) in the  folder.

```bash
$ cd example
$ npm install
$ react-native run-android
```

## Other
Project fork's [react-native-recyclerview-list](https://github.com/godness84/react-native-recyclerview-list)

