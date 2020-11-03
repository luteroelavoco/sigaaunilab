import React, { useEffect } from "react";
import {
  View,
  TouchableOpacity,
  Text,
  StyleSheet,
  BackHandler,
} from "react-native";

const styles = StyleSheet.create({
  $hide: {
    display: "none",
  },
  container: {
    height: 40,
    backgroundColor: "#404E82",
    flexDirection: "row",
    justifyContent: "space-around",
    alignItems: "center",
  },
  button: {
    color: "#fff",
  },
  buttonTitle: {
    color: "#fff",
    fontSize: 16,
  },
});

const NavigationView = ({
  onBackPress,
  onForwardPress,
  onHomePress,
  canGoBack,
  canGoForward,
}) => {
  useEffect(() => {
    BackHandler.addEventListener('hardwareBackPress', onBackPress);
    return () => {
      BackHandler.removeEventListener('hardwareBackPress', onBackPress);
    };
  }, []);
  
  return (
    <View style={[styles.container, !canGoBack && styles.$hide]}>
       {canGoBack && (
        <TouchableOpacity style={styles.button} onPress={onBackPress}>
          <Text style={styles.buttonTitle}>Voltar</Text>
        </TouchableOpacity>
      )}
      <TouchableOpacity style={styles.button} onPress={onHomePress}>
        <Text style={styles.buttonTitle}>Home</Text>
      </TouchableOpacity>
      
    </View>
  );
};

export default NavigationView;
