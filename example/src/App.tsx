/* eslint-disable @typescript-eslint/no-unused-vars */
import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  NativeModules,
  NativeEventEmitter,
  Button,
} from 'react-native';

import SmartMultitestSdkManager from 'react-native-smart-multitest-sdk';

var sdkManager = NativeModules.SmartMultitestSdk;
const sdkManagerEmitter = new NativeEventEmitter(sdkManager);

export default function App() {
  const [result] = React.useState<number | undefined>();

  React.useEffect(() => {
    // call here
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
