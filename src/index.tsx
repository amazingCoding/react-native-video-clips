import { NativeModules } from 'react-native';
export interface VideoRes {
  url: string,
  type: string,
  thum: string
  name:string,
  cancel: boolean,
}
export interface VideoCompressionRes {
  videoPath: string,
  url: string,
}
type VideoClipsType = {
  select(): Promise<VideoRes>;
  compression(name: string): Promise<VideoCompressionRes>;
};

const { VideoClips } = NativeModules;

export default VideoClips as VideoClipsType;
