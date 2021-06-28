import * as React from 'react'
import { StyleSheet, View, Text, TouchableOpacity, Image, Platform, PermissionsAndroid } from 'react-native'
import VideoClips, { VideoRes } from 'react-native-video-clips'

export default function App() {
  const [result, setResult] = React.useState<VideoRes | null>(null);

  const getVideo = React.useCallback(async () => {
    if (Platform.OS === 'android') {
      await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        {
          title: "WRITE_EXTERNAL_STORAGE",
          message:
            "Cool Photo App needs access to your camera " +
            "so you can take awesome pictures.",
          buttonNeutral: "Ask Me Later",
          buttonNegative: "Cancel",
          buttonPositive: "OK"
        })
    }

    try {
      const res = await VideoClips.select()
      console.log(res);
      if (!res.cancel) {
        setResult(res)
      }
    } catch (error) {

    }
  }, [])

  return (
    <View style={styles.container}>
      <TouchableOpacity style={{ backgroundColor: '#ff00ff' }} onPress={getVideo}>
        <Text>Result: {result?.url}</Text>
      </TouchableOpacity>
      {result?.thum !== '' ? <Image resizeMethod="scale" style={{ width: 50, height: 100, marginTop: 20 }} source={{ uri: result?.thum }}></Image> : null}
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
