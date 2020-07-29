
# react-native-recyclerview

## Getting started

`$ npm install react-native-recyclerview --save`

### Mostly automatic installation(react-native < 0.60)

`$ react-native link react-native-recycleview`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.quenice.cardview.RNRecycleviewPackage;` to the imports at the top of the file
  - Add `new RNRecycleviewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-recycleview'
  	project(':react-native-recycleview').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-recycleview/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-recycleview')
  	```


## Usage
```javascript
import {RecyclerView,DataSource} from 'react-native-android-recycleview';

<RecyclerView
    ref={(component) => this._recycler = component}
    style={{ flex: 1 }}
    dataSource={dataSource}
    renderItem={this.renderItem}
    windowSize={50}
    initialScrollIndex={0}
    inverted={false}
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
    )} />
```
