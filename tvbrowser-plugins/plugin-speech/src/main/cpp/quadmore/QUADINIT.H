#if !defined	__QUADINIT_H__
#define			__QUADINIT_H__

class QUADSAPI
{
public:

	// Let's assume this function uses the QUADSAPI,
	// after it has been initialized.
	void	UseQUADSAPI();

	// This nested class will do the QUADSAPI initialization
	// and uninitialization, once and for all
	class SetUpQUADSAPI
	{
	public:
		// creator
		SetUpQUADSAPI();

		// destructor
		~SetUpQUADSAPI();
	};
};

#endif
