import { NativeModules } from 'react-native';
export interface VideoRes {
  url: string,
  type: string,
  thum: string
  cancel: boolean,
}
type VideoClipsType = {
  select(): Promise<VideoRes>;
};

const { VideoClips } = NativeModules;

export default VideoClips as VideoClipsType;
