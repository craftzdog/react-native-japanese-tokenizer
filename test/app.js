/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  TextInput,
  View,
  Button
} from 'react-native';
import Tokenizer from 'react-native-japanese-tokenizer'

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {
  constructor(props) {
    super(props);
    this.state = { text: '週休七日で働きたい', result: false };
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>Japanese Tokenizer Demo</Text>
        <TextInput
          style={styles.input}
          onChangeText={(text) => this.setState({text})}
          value={this.state.text}
        />
        <Button title="Tokenize" onPress={this.handlePress} />
        { this.state.result && (
          <Text style={styles.result}>{this.state.result.join(' / ')}</Text>
        ) }
      </View>
    );
  }

  handlePress = async () => {
    const tokens = await Tokenizer.tokenize(this.state.text)
    console.log('tokens:', tokens)
    this.setState({ result: tokens })
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 20
  },
  input: {
    padding: 6,
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    fontSize: 20,
    width: 300
  },
  result: {
    margin: 10,
    borderColor: 'gray',
    borderWidth: 1,
    padding: 8
  }
});
