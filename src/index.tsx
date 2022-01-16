import { NativeModules } from 'react-native';
var sdkManager = NativeModules.SmartMultitestSdk;
class SmartMultitestSdkManager {
  constructor() {
    // this.isPeripheralConnected = this.isPeripheralConnected.bind(this);
  }

  startSdk(): Promise<void | string> {
    return new Promise((fulfill, reject) => {
      sdkManager.start((error: any) => {
        if (error) {
          reject(error);
        } else {
          fulfill();
        }
      });
    });
  }
}

export default new SmartMultitestSdkManager();
