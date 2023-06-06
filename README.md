# ZusExampleAndroid

This is a test project for the Zus Android SDK. In this app you can find examples of how to use the
SDK.

The app covers demo for two product lines of Zus i.e. bolt and vult.

## Bolt

It is a cryptocurrency wallet for exchange of zcn ERC 20 and ethereum tokens. Tokens can also be
staked and users get rewards for staking.

## Vult

Vult is a dencentralised anonymous file sharing platform. Users can upload files and share them with
other users.

File Uploads: mobilesdk method zbox.Zbox.multiUpload() is used to upload multiple files simultaneously.
More details on params used is mentioned [here](https://github.com/0chain/gosdk/blob/7f9eea986a87697ad13710731f4ba350b4a47c08/mobilesdk/zbox/storage.go#L275)

File Download: mobilesdk method zbox.Zbox.downloadFile() is used to download the desired file.
More details on params used is mentioned [here](https://github.com/0chain/gosdk/blob/7f9eea986a87697ad13710731f4ba350b4a47c08/mobilesdk/zbox/storage.go#L198)

The file can either be downloaded in the app specific storage (Making Available Offline) or in the phone's downloads directory (Downloading). In HelloWorld-Android, to demonstrate both of these, first, the file is downloaded in app specific storage and then a copy is made into the phone's download directory.

Thumbnail Download:  mobilesdk method zbox.Zbox.downloadThumbnail() is used to download the desired file.
More details on params used is mentioned [here](https://github.com/0chain/gosdk/blob/7f9eea986a87697ad13710731f4ba350b4a47c08/mobilesdk/zbox/storage.go#L237)

## SDK

Both of the apps rely on the [gosdk](https://github.com/0chain/gosdk) for interacting with the
0chain blockchain.

## Setup the SDK

### Download the latest sdk from [here](https://github.com/0chain/gosdk/releases). Extract `zcncore.aar`

1. Now copy `zcncore.aar` in `app/libs` directory. In your
2. In your app `build.gradle` add this implementation to add the `gosdk` in your dependencies.

   `implementation fileTree(dir: 'libs', include: ['*.aar'])`
3. Now extract the `zcncore.aar` and copy the `jni` libraries to `app/src/main/jniLibs`
4. Sync the project again.

### Load the libaries

To load the libraies in your application on runtime create an `Application` and inside this add
this.

```kotlin
init {
    System.loadLibrary("c++_shared")
    System.loadLibrary("bls384")
    System.loadLibrary("gojni")
}   
```

Now we also need to initialize the `Zcncore` in our `Application` class.

```kotlin
Zcncore.init(configJsonString)
```

configJson is the json string which contains the configuration for the sdk.

```json
 {
  "config": {
    "signature_scheme": "bls0chain",
    "block_worker": "https://demo.zus.network/dns",
    "confirmation_chain_length": 3
  },
  "data_shards": 2,
  "parity_shards": 2,
  "zbox_url": "https://0box.demo.zus.network/",
  "block_worker": "https://demo.zus.network",
  "domain_url": "demo.zus.network",
  "network_fee_url": "https://demo.zus.network/miner01/v1/block/get/fee_stats",
  "explorer_url": "https://demo.zus.network/"
}
```

## How to create a wallet.
To create a wallet you need to call the `Zcncore.createWalletOffline()` function.
## Some common terms used in our code and blockchain

- `blobber` - A blobber is a storage provider. It is a server that stores files on behalf of users.
  Blobbers are paid for storing files and for serving files to users. Blobbers are also paid for
  serving files to other blobbers. Blobbers are paid in ZCN tokens.
- `allocation` - An allocation is a group of blobbers that are used to store files. An allocation
  has a set of parameters that define how files are stored and how blobbers are paid. An allocation
  is paid in ZCN tokens.
- `miners` - Miners are the nodes that run the 0chain blockchain. Miners are paid in ZCN tokens.
- `sharders` - Sharders are the nodes that run the 0chain blockchain. Sharders are paid in ZCN
  tokens.
- `wallet` - A wallet is a collection of keys that are used to sign transactions. A wallet is used
  to sign transactions for blobbers, miners, sharders, and users.
- `ZCN` - ZCN is the token that is used to pay miners, sharders, blobbers, and users.
- `ERC20` - ERC20 is the token format used by zcn and ethereum.
- `public key` - A public key is a key that is used to verify a signature. A public key is used to
  verify a signature for a transaction.
- `private key` - A private key is a key that is used to sign a transaction. A private key is used
  to sign a transaction for a blobber, miner, sharder, or user.
- `signature` - A signature is a string that is used to verify that a transaction was signed by a
  private key. A signature is used to verify that a transaction was signed by a private key for a
  blobber, miner, sharder, or user.
- `mnemonics` - Mnemonics are a set of words that are used to generate a wallet. Mnemonics are used
  to generate a wallet for a user.

## Hackathon Discord Link
https://discord.gg/7JSzwpcK55

## A short video clip conveying the features of HelloWorld-Android app
https://github.com/0chain/HelloWorld-Android/assets/121080641/93fe80c9-423b-4d02-9b4a-6a20b6146420


