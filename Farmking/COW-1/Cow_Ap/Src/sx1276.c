#define _SX1276_C_

#include <string.h>
#include "stm32l1xx_hal.h"
#include "sx1276.h"
#include <stdint.h>
#include "uart1.h"


SX1276_s _SX1276 = 
{
	SX1276Init,
	SX1276StartTx,
	SX1276StartRx,
	SX1276Process,
	SX1276Reset,
	0,			//SX1276->Rx.Buf 0으로 초기화 
	STATE_IDLE
};
SX1276_s *SX1276 = &_SX1276;
uint8_t SX1276Regs[0x70] = {0};
SX1276Reg_Lora *SX1276RegLora = (SX1276Reg_Lora *)SX1276Regs;

#define LORA_FREQ (923300000)//923.3Mhz
void SX1276Init()
{
	uint32_t SetFrequency = LORA_FREQ;
	HAL_GPIO_WritePin(RF_NSS_GPIO_Port, RF_NSS_Pin, GPIO_PIN_SET);
	SX1276->Reset();
	
	SX1276SetOpMode(RFLR_OPMODE_SLEEP);
	
	//For Band Specific Additional Registers 
	if( SetFrequency > RF_MID_BAND_THRESH ){ SX1276RegLora->RegOpMode = RFLR_OPMODE_LONGRANGEMODE_ON|RFLR_OPMODE_FREQMODE_ACCESS_LF; }
	else { SX1276RegLora->RegOpMode = RFLR_OPMODE_LONGRANGEMODE_ON|RFLR_OPMODE_FREQMODE_ACCESS_HF; }
	SX1276Write(REG_LR_OPMODE, SX1276RegLora->RegOpMode);

	//Default Explicit Mode ( Header 포함)?
	//정보의 바이트 수(NbByte),coding rate(최대 4/8),CRC가 포함되어 있음

	
	SX1276SetOpMode(RFLR_OPMODE_STANDBY);
	SX1276SetFreq(SetFrequency);
	SX1276SetTxPower(15);
	SX1276SetBandWidth(125);
	SX1276SetHeader(0);
	SX1276SetCodingRate(1);
	SX1276SetSpreadingFactor(7);
	SX1276SetCrc(1);
	SX1276SetSymbTimeout(0x3FF);
	SX1276SetPayloadMaxLength(0xFF);
	SX1276SetPayloadLength(0xFF);
	SX1276SetLowDatarateOptimize(1);
	SX1276->StartRx();
	

	
}
void SX1276StartTx(uint8_t *TxBuf,uint8_t TxBufSize)
{
	
	SX1276SetOpMode(RFLR_OPMODE_STANDBY);

	// Set Tx buffer size
	SX1276RegLora->RegPayloadLength = TxBufSize;
	SX1276Write( REG_LR_PAYLOADLENGTH, SX1276RegLora->RegPayloadLength);

    // Full buffer used for Tx
    SX1276RegLora->RegFifoTxBaseAddr = 0;
    SX1276Write( REG_LR_FIFOTXBASEADDR, SX1276RegLora->RegFifoTxBaseAddr);
	SX1276RegLora->RegFifoAddrPtr = 0;
    SX1276Write( REG_LR_FIFOADDRPTR, SX1276RegLora->RegFifoAddrPtr);

                             	    // TxDone               RxTimeout                   FhssChangeChannel          ValidHeader         
    SX1276RegLora->RegDioMapping1 = RFLR_DIOMAPPING1_DIO0_01 | RFLR_DIOMAPPING1_DIO1_00 | RFLR_DIOMAPPING1_DIO2_00 | RFLR_DIOMAPPING1_DIO3_01;
                                    // PllLock              Mode Ready
    SX1276RegLora->RegDioMapping2 = RFLR_DIOMAPPING2_DIO4_01 | RFLR_DIOMAPPING2_DIO5_00;
    SX1276WriteBuffer( REG_LR_DIOMAPPING1, &SX1276RegLora->RegDioMapping1, 2 );
		
    SX1276WriteFifo( TxBuf, SX1276RegLora->RegPayloadLength );
	SX1276SetOpMode( RFLR_OPMODE_TRANSMITTER );
	SX1276->State = STATE_TX_RUNNING;
	
}
void SX1276StartRx(void)
{
	SX1276SetOpMode( RFLR_OPMODE_STANDBY );

                          		   // RxDone                    RxTimeout                   FhssChangeChannel           CadDone
    SX1276RegLora->RegDioMapping1 = RFLR_DIOMAPPING1_DIO0_00 | RFLR_DIOMAPPING1_DIO1_00 | RFLR_DIOMAPPING1_DIO2_00 | RFLR_DIOMAPPING1_DIO3_00;
                                  // CadDetected               ModeReady
    SX1276RegLora->RegDioMapping2 = RFLR_DIOMAPPING2_DIO4_00 | RFLR_DIOMAPPING2_DIO5_00;
    SX1276WriteBuffer( REG_LR_DIOMAPPING1, &SX1276RegLora->RegDioMapping1, 2 );

	memset( (void *)SX1276->RxBuf, 0,  sizeof(SX1276->RxBuf) );
	
	SX1276RegLora->RegFifoAddrPtr = SX1276RegLora->RegFifoRxBaseAddr;
    SX1276Write( REG_LR_FIFOADDRPTR, SX1276RegLora->RegFifoAddrPtr );
    
    SX1276SetOpMode( RFLR_OPMODE_RECEIVER );
	SX1276->State = STATE_RX_RUNNING;
	
}
uint8_t testReult = 0;

void SX1276Process(void)
{
	uint8_t DIO0 = 0;
	switch(SX1276->State)
	{
		case STATE_IDLE:
			break;
		case STATE_TX_RUNNING:
			DIO0 = HAL_GPIO_ReadPin(DIO0_GPIO_Port,DIO0_Pin);
			if( DIO0 == 1 ) // TxDone
			{
				// Clear Irq
           		 SX1276Write( REG_LR_IRQFLAGS, RFLR_IRQFLAGS_TXDONE );
           		 SX1276->State = STATE_TX_DONE;   
			}
			break;
		case STATE_TX_DONE:
			SX1276->StartRx();
			break;
		case STATE_RX_RUNNING:
			DIO0 = HAL_GPIO_ReadPin(DIO0_GPIO_Port,DIO0_Pin);
			if( DIO0 == 1 ) // RxDone
      		{
	            // Clear Irq
	            SX1276Write( REG_LR_IRQFLAGS, RFLR_IRQFLAGS_RXDONE  );
	            SX1276->State = STATE_RX_DONE;
      		}
			break;
		case STATE_RX_DONE:
			SX1276RegLora->RegIrqFlags = SX1276Read( REG_LR_IRQFLAGS );
			//testReult = SX1276Read(REG_LR_HOPCHANNEL);
			if( ( SX1276RegLora->RegIrqFlags & RFLR_IRQFLAGS_PAYLOADCRCERROR ) == RFLR_IRQFLAGS_PAYLOADCRCERROR )
			{
				// Clear Irq
       			 SX1276Write( REG_LR_IRQFLAGS, RFLR_IRQFLAGS_PAYLOADCRCERROR  );
			}
			else
			{
				SX1276RegLora->RegNbRxBytes = SX1276Read( REG_LR_RXNBBYTES );
				SX1276ReadFifo( SX1276->RxBuf, SX1276RegLora->RegNbRxBytes );
				uint16_t i;
				#if 0
				for(i=0;i<SX1276RegLora->RegNbRxBytes;i++)
				{
						Uart1_PutChar(SX1276->RxBuf[i]); // 무조건 로라 데이터 수신시 Uart로 전송한다. 
														// 만약에 데이터를 버퍼에 집어 넣고 버퍼 데이터를 처리하는 도중에 SX1276 RX 이벤트가 발생하고
													   // RX된 데이터가 버퍼를 덮어 싀우게 되면 데이터가 손실되는 경우가 잇음
													  // 아니면 버퍼에 집어 넣고 다 처리 햇을 경우에만 SX1276->StartRx();를 실행하면 안전함
						//Lora_RxProcess(SX1276->RxBuf[i]);
				}
				#endif
				//수신받은 데이터를 옮겨 담을 코드가 필요함 
			}
			SX1276->StartRx();
	        break;
		default:
			break;
	}
}
void SX1276Reset(void)
{
	HAL_GPIO_WritePin(RF_NRESET_GPIO_Port, RF_NRESET_Pin, GPIO_PIN_RESET);
	HAL_Delay(1);
	HAL_GPIO_WritePin(RF_NRESET_GPIO_Port, RF_NRESET_Pin, GPIO_PIN_SET);
	HAL_Delay(6);
}
void SX1276SetOpMode(uint8_t OpMode)
{
	if ((OpMode == RFLR_OPMODE_TRANSMITTER) || (OpMode == RFLR_OPMODE_SYNTHESIZER_TX))
	{
	//	printf("TX Running..\r\n");
		HAL_GPIO_WritePin(RF_SWITCH_GPIO_Port, RF_SWITCH_Pin, GPIO_PIN_RESET); // tx mode
		//HAL_GPIO_WritePin(RF_RXTX_GPIO_Port, RF_RXTX_Pin, GPIO_PIN_SET);
	}
	else if ((OpMode == RFLR_OPMODE_SYNTHESIZER_RX) || (OpMode == RFLR_OPMODE_RECEIVER))
	{
	//	printf("RX Running..\r\n");
		HAL_GPIO_WritePin(RF_SWITCH_GPIO_Port, RF_SWITCH_Pin, GPIO_PIN_SET); // rx mode 
		//HAL_GPIO_WritePin(RF_RXTX_GPIO_Port, RF_RXTX_Pin, GPIO_PIN_RESET);
	}
	SX1276Write(REG_LR_OPMODE, (SX1276Read(REG_LR_OPMODE) & RFLR_OPMODE_MASK) | OpMode);
}

void SX1276Write( uint8_t addr, uint8_t data )
{
    SX1276WriteBuffer( addr, &data, 1 );
}

uint8_t SX1276Read( uint8_t addr )
{
    uint8_t data;
    SX1276ReadBuffer( addr, &data, 1 );
    return data;
}

void SX1276WriteBuffer( uint8_t addr, uint8_t *buffer, uint8_t size )
{
	uint8_t i;
	HAL_GPIO_WritePin(RF_NSS_GPIO_Port, RF_NSS_Pin, GPIO_PIN_RESET);
	HAL_Delay(1);
	addr = addr|0x80;
	SX1276SpiOut(addr);
    for( i = 0; i < size; i++ )
    {
        SX1276SpiOut( buffer[i] );
    }
	HAL_Delay(1);
	HAL_GPIO_WritePin(RF_NSS_GPIO_Port, RF_NSS_Pin, GPIO_PIN_SET);
}

void SX1276ReadBuffer( uint8_t addr, uint8_t *buffer, uint8_t size )
{
    uint8_t i;
	HAL_GPIO_WritePin(RF_NSS_GPIO_Port, RF_NSS_Pin, GPIO_PIN_RESET);
	HAL_Delay(1);
	addr = addr & 0x7F;
	SX1276SpiOut(addr);
	for( i = 0; i < size; i++ )
	{
		SX1276SpiIn( &buffer[i] );
	}
	HAL_Delay(1);
	HAL_GPIO_WritePin(RF_NSS_GPIO_Port, RF_NSS_Pin, GPIO_PIN_SET);
}

void SX1276SpiOut( uint8_t data )
{
	uint8_t bit_mask;
	HAL_GPIO_WritePin(RF_SCK_GPIO_Port, RF_SCK_Pin, GPIO_PIN_RESET);
	for(bit_mask = 0x80; bit_mask > 0x00 ; bit_mask >>=1)
	{
		if((bit_mask & data) == bit_mask)		{HAL_GPIO_WritePin(RF_MOSI_GPIO_Port,RF_MOSI_Pin,GPIO_PIN_SET);}	//Data_H
		else									{HAL_GPIO_WritePin(RF_MOSI_GPIO_Port,RF_MOSI_Pin,GPIO_PIN_RESET);}	//Data_L
		HAL_GPIO_WritePin(RF_SCK_GPIO_Port,RF_SCK_Pin,GPIO_PIN_SET);				//CLK_H
		HAL_GPIO_WritePin(RF_SCK_GPIO_Port,RF_SCK_Pin,GPIO_PIN_RESET);				//CLK_L
	}
}
void SX1276SpiIn( uint8_t *data )
{
	uint8_t bit_mask;
	HAL_GPIO_WritePin(RF_SCK_GPIO_Port,RF_SCK_Pin,GPIO_PIN_RESET);					//CLK_L
	*data = 0;
	for(bit_mask = 0x80; bit_mask > 0x00 ; bit_mask >>=1)
	{
		HAL_GPIO_WritePin(RF_SCK_GPIO_Port,RF_SCK_Pin,GPIO_PIN_SET);				//CLK_H
		if(HAL_GPIO_ReadPin(RF_MISO_GPIO_Port,RF_MISO_Pin) == GPIO_PIN_SET)		{*data |= bit_mask;}	//Data_H
		HAL_GPIO_WritePin(RF_SCK_GPIO_Port,RF_SCK_Pin,GPIO_PIN_RESET);				//CLK_L
	}
}

void SX1276WriteFifo( uint8_t *buffer, uint8_t size )
{
    SX1276WriteBuffer( 0, buffer, size );
}

void SX1276ReadFifo( uint8_t *buffer, uint8_t size )
{
    SX1276ReadBuffer( 0, buffer, size );
}
// if   freq < 525Mhz then freq is LF
// else freq is HF
void SX1276SetFreq( uint32_t freq )
{
    freq = ( uint32_t )( ( double )freq / ( double )FREQ_STEP );
	
	SX1276RegLora->RegFrfMsb = ( ( freq >> 16 ) & 0xFF ) ;
	SX1276RegLora->RegFrfMid = ( ( freq >> 8 ) & 0xFF );
	SX1276RegLora->RegFrfLsb = ( freq & 0xFF );
	
    SX1276Write( REG_LR_FRFMSB, SX1276RegLora->RegFrfMsb );
    SX1276Write( REG_LR_FRFMID, SX1276RegLora->RegFrfMid );
    SX1276Write( REG_LR_FRFLSB, SX1276RegLora->RegFrfLsb );
}

uint32_t SX1276GetFreq(void)
{
	uint32_t freq=0;
	freq |= (SX1276Read(REG_LR_FRFMSB)<<16);
	freq |= (SX1276Read(REG_LR_FRFMID)<<8);
	freq |= SX1276Read(REG_LR_FRFLSB);

	freq = (double)freq * (double)FREQ_STEP;
	return freq;
}

// power range 0 ~ 15
void SX1276SetTxPower( int8_t power )
{
    uint8_t paConfig = 0;
    if( power > 15 ) { power = 15; }
    paConfig = 0x70;//(Pmax=10.8+0.6*MaxPower [dBm])
    paConfig |= power & 0x0F;//하위 4비트를 연산하여 TxPower를 조절함 
    
    SX1276Write( REG_LR_PACONFIG, paConfig );
}
// OnlyLora
// 125 : 125Khz
// 250 : 250Khz
// 500 : 500Khz

void SX1276SetBandWidth(uint16_t bandWidth)
{
	uint8_t temp = 0;
	temp = SX1276Read(REG_LR_MODEMCONFIG1);
	temp = temp & 0x0F;
	switch(bandWidth)
	{
		case 125: //0x70
			temp |= 0x70;
			break;
		case 250: //0x80
			temp |= 0x80;
			break;
		case 500: //0x90
			temp |= 0x90;
			break;
		default:
			return ;
	}
	
	SX1276RegLora->RegModemConfig1 = temp;
	SX1276Write(REG_LR_MODEMCONFIG1,SX1276RegLora->RegModemConfig1);
}

// 1: 4/5
// 2: 4/6
// 3: 4/7
// 4: 4/8
void SX1276SetCodingRate(uint8_t codingRate)
{
	uint8_t temp = 0;
	temp = SX1276Read(REG_LR_MODEMCONFIG1);
	temp = temp & 0xF1;
	switch(codingRate)
	{
		case 1: // 4/5
			temp |= 0x02;
			break;
		case 2: // 4/6
			temp |= 0x04;
			break;
		case 3: // 4/7
			temp |= 0x06;
			break;
		case 4: // 4/8
			temp |= 0x08;
			break;
		default:
			return ;
	}
	
	SX1276RegLora->RegModemConfig1 = temp;
	SX1276Write(REG_LR_MODEMCONFIG1,SX1276RegLora->RegModemConfig1);
}

//   6(only Implicit Header Mode )
//..12
void SX1276SetSpreadingFactor(uint8_t SF)
{
	uint8_t temp = 0;
	temp = SX1276Read(REG_LR_MODEMCONFIG2);
	temp = temp & 0x0F;
	switch(SF)
	{
		case 6: 
			temp |= 0x60;
			break;
		case 7: 
			temp |= 0x70;
			break;
		case 8: 
			temp |= 0x80;
			break;
		case 9: 
			temp |= 0x90;
			break;
		case 10: 
			temp |= 0xA0;
			break;
		case 11: 
			temp |= 0xB0;
			break;
		case 12:  
			temp |= 0xC0;
			break;
		default:
			return ;
	}
	
	SX1276RegLora->RegModemConfig2 = temp;
	SX1276Write(REG_LR_MODEMCONFIG2,SX1276RegLora->RegModemConfig2);
}

// 0:OFF
// 1:ON
void SX1276SetCrc(uint8_t crcFlag)
{
	uint8_t temp = 0;
	temp = SX1276Read(REG_LR_MODEMCONFIG2);
	temp = temp & 0xFB;
	switch(crcFlag)
	{
		case 0: 
			temp |= 0x02;
			break;
		case 1: 
			temp |= 0x04;
			break;
		default:
			return ;
	}
	
	SX1276RegLora->RegModemConfig2 = temp;
	SX1276Write(REG_LR_MODEMCONFIG2,SX1276RegLora->RegModemConfig2);
}

// 0: Explicit Header mode (default)
// 1: Implicit Header mode
void SX1276SetHeader(uint8_t HeaderMode)
{
	uint8_t temp = 0;
	temp = SX1276Read(REG_LR_MODEMCONFIG1);
	temp = temp & 0xFE;
	
	switch(HeaderMode)
	{
		case 0: 
			temp |= 0x00;
			break;
		case 1: 
			temp |= 0x01;
			break;
		default:
			return ;
	}
	
	SX1276RegLora->RegModemConfig1 = temp;
	SX1276Write(REG_LR_MODEMCONFIG1,SX1276RegLora->RegModemConfig1);
}
void SX1276GetPacketRssi(int16_t *data)
{
	uint8_t temp=0;
	temp = SX1276Read(REG_LR_RSSIVALUE);

	if( LORA_FREQ > RF_MID_BAND_THRESH )
	{
		*data = -157 + temp;
	}
	else
	{
		*data = -164 + temp;
	}
}
void SX1276SetSymbTimeout( uint16_t value )
{
    SX1276ReadBuffer( REG_LR_MODEMCONFIG2, &SX1276RegLora->RegModemConfig2, 2 );

    SX1276RegLora->RegModemConfig2 = ( SX1276RegLora->RegModemConfig2 & RFLR_MODEMCONFIG2_SYMBTIMEOUTMSB_MASK ) | ( ( value >> 8 ) & ~RFLR_MODEMCONFIG2_SYMBTIMEOUTMSB_MASK );
    SX1276RegLora->RegSymbTimeoutLsb = value & 0xFF;
    SX1276WriteBuffer( REG_LR_MODEMCONFIG2, &SX1276RegLora->RegModemConfig2, 2 );
}
void SX1276SetPayloadLength( uint8_t value )
{
    SX1276RegLora->RegPayloadLength = value;
    SX1276Write( REG_LR_PAYLOADLENGTH, SX1276RegLora->RegPayloadLength );
}
void SX1276SetPayloadMaxLength( uint8_t value )
{
    SX1276RegLora->RegMaxPayloadLength = value;
    SX1276Write( REG_LR_PAYLOADMAXLENGTH, SX1276RegLora->RegMaxPayloadLength );
}
void SX1276SetLowDatarateOptimize(uint8_t enable )
{
    SX1276ReadBuffer( REG_LR_MODEMCONFIG3, &SX1276RegLora->RegModemConfig3,1 );
    SX1276RegLora->RegModemConfig3 = ( SX1276RegLora->RegModemConfig3 & RFLR_MODEMCONFIG3_LOWDATARATEOPTIMIZE_MASK ) | ( enable << 3 );
    SX1276Write( REG_LR_MODEMCONFIG3, SX1276RegLora->RegModemConfig3 );
}



