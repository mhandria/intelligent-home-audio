// Initialize SysTick periodic interrupts
// Assumes 80Mhz clk
void SysTick_Init(void)
{
    // Calculate how many clock cycles between interrupts
    // unsigned long period = 80000000 / SAMPLE_FREQ;
    // 44,100Hz = 22.67574us period
    // 80,000,000 = 0.0125us period
    // 22.675 / 0.0125 = 1814 clk ticks
    
  NVIC_ST_CTRL_R = 0;         // disable SysTick during setup
  NVIC_ST_RELOAD_R = 1814-1;// reload value
  NVIC_ST_CURRENT_R = 0;      // any write to current clears it
  NVIC_SYS_PRI3_R = (NVIC_SYS_PRI3_R&0x00FFFFFF)|0x40000000; // priority 2
                              // enable SysTick with core clock and interrupts
  NVIC_ST_CTRL_R = 0x07;
}

// Interrupt service routine
void SysTick_Handler(void)
{
    writeDAC(readBuff()); // Update the DAC to match the current sample
    // unsigned char out = sampleBuffer[sampleReadPtr];
}