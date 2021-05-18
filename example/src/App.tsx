import * as React from 'react';

import {
  StyleSheet,
  Image,
  Dimensions,
  TouchableOpacity,
  Text,
  SafeAreaView,
} from 'react-native';
import MediaPicker from '../../src/index';

const { width } = Dimensions.get('window');

export default function App() {
  const [photos, setPhotos] = React.useState([]);

  const onAddPhoto = async () => {
    const photos = await MediaPicker.openGallery({
      maxAllowedBytes: 4194304,
      maxFiles: 4,
      tcSupport: true,
    });
    setPhotos(photos);
    console.log(photos);
  };

  return (
    <SafeAreaView style={styles.container}>
      {photos.map((photo, index) => {
        const { path } = photo;
        console.warn(path);
        return (
          <Image key={index} source={{ uri: path }} style={styles.image} />
        );
      })}
      <TouchableOpacity onPress={onAddPhoto} style={styles.button}>
        <Text style={styles.plus}>+</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'flex-start',
    justifyContent: 'flex-start',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  image: {
    width: width / 3,
    height: width / 3,
  },
  button: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#4267B2',
    justifyContent: 'center',
    alignContent: 'center',
    position: 'absolute',
    bottom: 12,
    right: 12,
  },
  plus: {
    textAlign: 'center',
    color: 'white',
    fontSize: 24,
    fontWeight: '700',
  },
});
