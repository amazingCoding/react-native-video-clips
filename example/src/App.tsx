import * as React from 'react'
import { StyleSheet, View, Text, TouchableOpacity, Image, Platform, PermissionsAndroid } from 'react-native'
import VideoClips, { VideoRes } from 'react-native-video-clips'
import { ImagePickerResponse, launchCamera } from "react-native-image-picker";

export default function App() {
  const [result, setResult] = React.useState<VideoRes | null>(null);
  const [loading, setLoading] = React.useState(false)

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
      <TouchableOpacity style={{ backgroundColor: '#ff00ff', marginBottom: 20, padding: 20, width: 200, alignItems: 'center' }} onPress={() => {
        launchCamera({ mediaType: 'video', durationLimit: 10, videoQuality: 'high' }, async (res: ImagePickerResponse) => {
          console.log('launchCamera', res);
          if (res.didCancel) return
          if (res.uri) {
            setLoading(true)
            let ts1 = new Date().getTime()
            try {
              const result = await VideoClips.compression(res.uri.replace('file://',''))
              let ts2 = new Date().getTime()
              console.log('launchCamera - success', result, Math.floor((ts2 - ts1) / 1000));
            } catch (error) {
              let ts2 = new Date().getTime()
              console.log('launchCamera - error', error, Math.floor((ts2 - ts1) / 1000));
            }
            finally {
              setLoading(false)
            }

          }
        })
      }}>
        <Text>{loading ? 'loading' : 'Add Video'}</Text>
      </TouchableOpacity>

      <TouchableOpacity style={{ backgroundColor: '#ff00ff', padding: 20, width: 200, alignItems: 'center' }} onPress={getVideo}>
        <Text>Video Clips</Text>
      </TouchableOpacity>
      <TouchableOpacity style={{ backgroundColor: '#ff00ff', marginBottom: 20, padding: 20, width: 200, alignItems: 'center' }} onPress={async () => {
        if (result?.name) {
          setLoading(true)
          let ts1 = new Date().getTime()
          try {
            const result2 = await VideoClips.compression(result?.name)
            let ts2 = new Date().getTime()
            console.log('launchCamera - success', result2, Math.floor((ts2 - ts1) / 1000));
          } catch (error) {
            let ts2 = new Date().getTime()
            console.log('launchCamera - error', error, Math.floor((ts2 - ts1) / 1000));
          }
          finally {
            setLoading(false)
          }

        }
      }}>
        <Text>{loading ? 'loading' : 'compression Video'}</Text>
      </TouchableOpacity>
      <Text style={{ marginTop: 20 }}>Result: {result?.url}</Text>
      {result?.thum !== '' ? <Image resizeMethod="scale" style={{ width: 50, height: 100, marginTop: 20 }} source={{ uri: result?.thum }}></Image> : null}
    </View >
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
