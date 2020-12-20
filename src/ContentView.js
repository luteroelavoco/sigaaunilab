import React, { useState, useRef } from "react";
import { View, StyleSheet, Linking } from "react-native";
import { WebView } from "react-native-webview";
import NavigationView from "./NavigationView";

const baseUrl = "https://sig.unilab.edu.br/sigaa/mobile/touch/login.jsf";

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

const shouldStartLoadWithRequest = (req) => {
  // open the link in native browser
  Linking.openURL(req.url);

  // returning false prevents WebView to navigate to new URL
  return false;
};
const ContentView = () => {
  const webviewref = useRef();
  const [canGoBack, setCanGoBack] = useState(false);
  const [canGoForward, setCanGoForward] = useState(false);
  const [url, setUrl] = useState(baseUrl);

  const handleBackPress = () => {
    webviewref.current.goBack();
  };

  const handleForwardPress = () => {
    webviewref.current.goForward();
    return true;
  };

  const handleHomePress = () => {
    setUrl(
      "https://sig.unilab.edu.br/sigaa/mobile/touch/menu.jsf" +
        "?t=" +
        Date.now()
    );
  };
  return (
    <View style={styles.container}>
      <WebView
        ref={webviewref}
        source={{
          uri: url,
        }}
        originWhitelist={["*"]}
        allowUniversalAccessFromFileURLs={true}
        allowFileAccessFromFileURLs={true}
        allowFileAccess={true}
        javaScriptEnabled={true}
        onNavigationStateChange={(state) => {
          const back = state.canGoBack;
          const forward = state.canGoForward;
          setCanGoBack(back);
          setCanGoForward(forward);
        }}
        // onShouldStartLoadWithRequest={shouldStartLoadWithRequest}
        sharedCookiesEnabled={true}
      />
      <NavigationView
        onBackPress={handleBackPress}
        onForwardPress={handleForwardPress}
        onHomePress={handleHomePress}
        canGoBack={canGoBack}
        canGoForward={canGoForward}
      />
    </View>
  );
};

export default ContentView;
